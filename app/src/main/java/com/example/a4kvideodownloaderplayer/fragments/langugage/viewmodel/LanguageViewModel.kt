import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.a4kvideodownloaderplayer.fragments.langugage.model.Languages
import com.example.a4kvideodownloaderplayer.helper.AppUtils

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("LanguagePreferences", Context.MODE_PRIVATE)

    private val _languagesList = MutableLiveData<List<Languages>>()
    val languagesList: LiveData<List<Languages>> = _languagesList

    private val _selectedLanguage = MutableLiveData<Languages>()
    val selectedLanguage: LiveData<Languages> get() = _selectedLanguage

    private val _selectedPosition = MutableLiveData<Int>()
    val selectedPosition: LiveData<Int> get() = _selectedPosition

    init {
        loadDefaultLanguages()
        restoreSelectedLanguage()  // Restore the saved selection
    }

    private fun loadDefaultLanguages() {
        _languagesList.value = AppUtils.getDefaultLanguages()
    }

    private fun restoreSelectedLanguage() {
        val savedPosition = sharedPreferences.getInt("selectedPosition", 0)
        val savedLanguageName = sharedPreferences.getString("selectedLanguageName", null)

        _selectedPosition.value = savedPosition

        _languagesList.value?.find { it.name == savedLanguageName }?.let { language ->
            _selectedLanguage.value = language
        }
    }

    fun selectLanguage(language: Languages, position: Int) {
        _selectedLanguage.value = language
        _selectedPosition.value = position
        saveSelectedLanguage(language, position)
    }

    private fun saveSelectedLanguage(language: Languages, position: Int) {
        sharedPreferences.edit().apply {
            putInt("selectedPosition", position)
            putString("selectedLanguageName", language.name)
            apply()
        }
    }
}
