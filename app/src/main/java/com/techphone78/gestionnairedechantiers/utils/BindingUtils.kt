package com.techphone78.gestionnairedechantiers.utils

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.entities.Couleur
import com.techphone78.gestionnairedechantiers.entities.Personnel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.math.roundToInt

@BindingAdapter("clickedElement")
fun setColorIfClicked(view: MaterialCardView, item: Boolean) {
    view.isChecked = item
}


@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {

//        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()

    Glide.with(imgView.context)
        .load(imgUrl)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.ic_business_black_24dp)
        )
//                .error(R.drawable.ic_broken_image))
        .into(imgView)
}

@BindingAdapter(value = ["imageUrl2", "typeEntity"], requireAll = false)
fun bindImage2(imgView: ImageView, imgUrl: String?, typeEntity: TypeEntity?) {

    Timber.i("typeEntity: $typeEntity")

    val placeholder: Int = when (typeEntity) {
        TypeEntity.MATERIEL -> R.drawable.ic_baseline_handyman_24
        TypeEntity.PERSONNEL -> R.drawable.ic_person_black_24dp
        TypeEntity.CHANTIER -> R.drawable.ic_business_black_24dp
        else -> {
            Timber.e("Error typeEntity")
            R.drawable.ic_person_black_24dp
        }
    }

    Glide.with(imgView.context)
        .load(imgUrl)
        .apply(
            RequestOptions()
                .placeholder(placeholder)
        )
        .into(imgView)
}

@BindingAdapter("imageUrlItemViewPersonnel")
fun bindImageItemViewPersonnel(imgView: ImageView, imgUrl: String?) {

    imgView.visibility = View.VISIBLE
    Glide.with(imgView.context)
        .load(imgUrl)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.ic_person_black_24dp)
        )
        .into(imgView)
}


@BindingAdapter("isButtonDeleteVisible")
fun activateButtonDelete(imgButton: ImageButton, imgUrl: String?) {
    imgButton.visibility = View.GONE

    imgUrl?.let {
        imgButton.visibility = View.VISIBLE
    }
}


@BindingAdapter("isButtonAddPictureVisible")
fun activateButtonAddPicture(imgButton: Button, imgUrl: String?) {

    if (imgUrl.isNullOrEmpty()) {
        imgButton.visibility = View.VISIBLE
    } else {
        imgButton.visibility = View.GONE
    }
}


@BindingAdapter("isPictureVisible")
fun showPicture(view: ConstraintLayout, imgUrl: String?) {

    if (imgUrl.isNullOrEmpty()) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("personnelRole")
fun setTextDependingPersonnelRole(textView: TextView, personnel: Personnel) {
    if (personnel.chefEquipe) {
        textView.text = "Chef d'équipe"
    } else {
        if (personnel.interimaire) {
            textView.text = "Intérimaire"
        } else {
            textView.text = "Employé"
        }
    }
}

@BindingAdapter("setCardVisibility")
fun setCardVisibility(cardView: MaterialCardView, value: Int) {
    when (value) {
        1 -> {
            Timber.i(" button.visibility = View.VISIBLE")
            cardView.visibility = View.VISIBLE
        }
        2 -> {
            Timber.i("  button.visibility = View.GONE")
            cardView.visibility = View.GONE

        }
    }
}


@BindingAdapter("setLayoutVisibility")
fun setLayoutVisibility(linearLayout: LinearLayout, value: Int) {
    when (value) {
        2 -> {
            Timber.i(" button.visibility = View.VISIBLE")
            linearLayout.visibility = View.VISIBLE
        }
        1 -> {
            Timber.i("  button.visibility = View.GONE")
            linearLayout.visibility = View.GONE

        }
    }
}

@BindingAdapter("setLayoutVisibility")
fun setLayoutVisibility(layout: ConstraintLayout, boolean: Boolean) {
    when (boolean) {
        true -> {
            Timber.i(" button.visibility = View.VISIBLE")
            layout.visibility = View.VISIBLE
        }
        false -> {
            Timber.i("  button.visibility = View.GONE")
            layout.visibility = View.GONE

        }
    }
}

@BindingAdapter("setTextFromFunction")
fun setTextFromFunction(textView: TextView, text: String) {
    textView.text = text
}


@BindingAdapter("showDate")
fun setTextFromDate(textView: TextView, date: Date?) {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

    if (date != null) {
        calendar.time = date
        textView.text = calendar.time.toString()
    }
}

@BindingAdapter("valueToCheckForMaxButton", "valueMax")
fun disableButtonMax(imageButton: ImageButton, value: Int, valueMax: Int) {
    if (value >= valueMax) {
        imageButton.isEnabled = false
        imageButton.isClickable = false
    } else {
        imageButton.isEnabled = true
        imageButton.isClickable = true
    }
}

@BindingAdapter("valueToCheckForMinButton")
fun disableButtonMin(imageButton: ImageButton, value: Int) {
    if (value <= 0) {
        imageButton.isEnabled = false
        imageButton.isClickable = false
    } else {
        imageButton.isEnabled = true
        imageButton.isClickable = true
    }
}


// PLUS TARD


@BindingAdapter("dataSaved", "dataEdited")
fun setBackgroundColor(cardView: MaterialCardView, booleanSaved: Boolean, dataEdited: Boolean) {
    val vert: Int = R.color.colorDataSavedTrue
    val rouge: Int = R.color.colorDataSavedFalse
    val orange: Int = R.color.colorDataEdited

    if (booleanSaved) {
        if (dataEdited) cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                cardView.context,
                orange
            )
        )
        else cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, vert))
    } else cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, rouge))
}


@BindingAdapter("input")
fun bindIntegerInText(
    editText: EditText,
    value: String
) {
    editText.setText(value)
    // Set the cursor to the end of the text
    editText.setSelection(editText.text!!.length)
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
            //inverseBindingListener.onChange();
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            Timber.i("TEXT CHANGED")


        }

        override fun afterTextChanged(s: Editable) {
            //inverseBindingListener.onChange();
            Timber.i("AFTER TEXT CHANGED")
        }
    })
}

@InverseBindingAdapter(attribute = "app:input", event = "app:inputAttrChanged")
fun bindCountryInverseAdapter(view: AppCompatEditText): Int {
    val string = view.text.toString()
    return if (string.isEmpty()) 0 else string.toInt()
}

//@BindingAdapter("scrollTo")
//fun scrollTo(scrollView: ScrollView, boolean: Boolean) {
//    Timber.i("Entree scrollTo")
//    if (boolean) {
//        Timber.i("scrollTo = True")
//        scrollView.scrollTo(0, R.id.editTextTextMultiLine)
//    } else {
//        Timber.i("scrollTo = False")
//    }
//}


@RequiresApi(Build.VERSION_CODES.O)
@BindingAdapter("date")
fun convertDateToTexView(textView: TextView, date: Instant?) {

//    val sdf = DateTimeFormatter.ofPattern(FormatStyle.SHORT, Locale.FRANCE)
    val sdf = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.FRANCE)
        .withZone(ZoneId.systemDefault())

    textView.text = date?.let { sdf.format(date) }
}

@BindingAdapter("date")
fun convertDateToEditTextField(textInputEditText: TextInputEditText, date: Date?) {

    if (date != null) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = date
        textInputEditText.setText(
            "${calendar.get(Calendar.DAY_OF_MONTH)}-${calendar.get(Calendar.MONTH) + 1}-${
                calendar.get(
                    Calendar.YEAR
                )
            } "
        )
    }
}

@BindingAdapter("layout_max_width")
fun setLayoutWidth(
    constraintLayout: ConstraintLayout,
    value: Int
) {
    val result = 180 + (value * 65)
    Timber.i("value width = $result")

    constraintLayout.maxWidth = dpToPx(result)

}

fun dpToPx(dp: Int): Int {
    val metrics = Resources.getSystem().displayMetrics.density
    return (dp.toFloat() * metrics).roundToInt()
}

@BindingAdapter("setGridLayout")
fun setGridLayout(recyclerView: RecyclerView, value: Int) {
    val manager = GridLayoutManager(recyclerView.context, value, GridLayoutManager.VERTICAL, false)
    recyclerView.layoutManager = manager
}


@BindingAdapter("typeChantier")
fun setTextTypeChantier(textView: TextView, value: Int) {
    if (value == 1) textView.text = "CHANTIER"
    else textView.text = "ENTRETIEN"
}


@BindingAdapter("adapter")
fun setAdapter(autoCompleteTextView: AutoCompleteTextView, items: List<String>) {
    val adapter = ArrayAdapter(autoCompleteTextView.context, R.layout.list_item, items)
    autoCompleteTextView.setAdapter(adapter)
}

@BindingAdapter("colorsAdapter")
fun setColorsAdapter(autoCompleteTextView: AutoCompleteTextView, colors: List<Couleur>?) {
    colors?.let {
        val items = mutableListOf<String>()
        for (couleur in it) {
            items.add(couleur.colorName!!)
        }
        val adapter = ArrayAdapter(autoCompleteTextView.context, R.layout.list_items_colors, items)
        autoCompleteTextView.setAdapter(adapter)
    }
}


@BindingAdapter("backgroundColor")
fun setBackGroundColor(view: View, color: String?) {
    view.background = null
    color?.let {
        view.setBackgroundColor(Color.parseColor(color))
    }
}