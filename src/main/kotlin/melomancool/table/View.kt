package melomancool.table

sealed class View<out Mes>

sealed class Element<Mes>: View<Mes>()
sealed class Layout<Mes>: View<Mes>()

data class BoxView<Mes>(
    val children: List<Element<out Mes>>
): Layout<Mes>()

data class TextFieldView<Mes>(
    val text: String,
    val onInput: ((String) -> Mes)? = null
): Element<Mes>()

data class ButtonView<Mes>(
    val text: String,
    val onClick: Mes? = null
): Element<Mes>()
