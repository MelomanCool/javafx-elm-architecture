package melomancool.table.jfx

import java.util.concurrent.BlockingQueue

import melomancool.table.TextFieldView
import melomancool.table.ButtonView


sealed class Node {
    abstract val jfx: javafx.scene.Node
}

sealed class Parent: Node() {
    abstract val children: List<Node>
}

data class Box(override val jfx: javafx.scene.layout.VBox, override val children: List<Node>): Parent()

data class TextField(val text: String, override val jfx: javafx.scene.control.TextField): Node() {
    companion object {
        fun <Mes> new(view: TextFieldView<Mes>, messageQueue: BlockingQueue<Mes>): TextField {
            val tfj = TextField(view.text, javafx.scene.control.TextField(view.text))
            if (view.onInput != null) {
                tfj.jfx.textProperty().addListener { _observable, _oldValue, newValue ->
                    messageQueue.offer(view.onInput!!(newValue))
                }
            }
            return tfj
        }
    }
}

data class Button(val text: String, override val jfx: javafx.scene.control.Button): Node() {
    companion object {
        fun <Mes> new(view: ButtonView<Mes>, messageQueue: BlockingQueue<Mes>): Button {
            val button = Button(view.text, javafx.scene.control.Button(view.text))
            if (view.onClick != null) {
                button.jfx.setOnAction { _event ->
                    messageQueue.offer(view.onClick!!)
                }
            }
            return button
        }
    }
}
