package melomancool.table.jfx

import java.util.concurrent.BlockingQueue

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

import melomancool.table.BoxView
import melomancool.table.ButtonView
import melomancool.table.TextFieldView
import melomancool.table.View

fun <Mes> makeTextField(view: TextFieldView<Mes>, messageQueue: BlockingQueue<Mes>): TextField {
    val tf = TextField(view.text)
    if (view.onInput != null) {
        tf.textProperty().addListener { _observable, _oldValue, newValue ->
            messageQueue.offer(view.onInput!!(newValue))
        }
    }
    return tf
}

fun <Mes> makeButton(view: ButtonView<Mes>, messageQueue: BlockingQueue<Mes>): Button {
    val button = Button(view.text)
    if (view.onClick != null) {
        button.setOnAction { _event ->
            messageQueue.offer(view.onClick!!)
        }
    }
    return button
}

fun <Mes> makeNode(view: View<Mes>, mq: BlockingQueue<Mes>): Node {
    return when(view) {
        is TextFieldView ->
            makeTextField(view, mq)
        is ButtonView ->
            makeButton(view, mq)
        is BoxView -> {
            val vbox = VBox()
            val nodes = view.children.map{ makeNode(it, mq) }
            vbox.getChildren().addAll(nodes)
            vbox
        }
    }
}

fun <Mes> replaceChild(
    view: View<Mes>,
    mq: BlockingQueue<Mes>,
    jfxParent: Pane
): Node {
    val child = makeNode(view, mq)
    jfxParent.getChildren().set(0, child)
    // jfxParent.getChildren().addAll(child)
    return child
}

fun <Mes> updateNode(
    view: View<Mes>,
    node: Node,
    mq: BlockingQueue<Mes>,
    jfxParent: Pane
): Node {
    return when (node) {
        is TextField ->
            when (view) {
                is TextFieldView -> {
                    Platform.runLater {
                        if (node.getText() != view.text) {
                            node.setText(view.text)
                        }
                    }
                    node
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        is Button ->
            when (view) {
                is ButtonView -> {
                    Platform.runLater {
                        if (node.getText() != view.text) {
                            node.setText(view.text)
                        }
                    }
                    node
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        is VBox ->
            when (view) {
                is BoxView ->
                    updateBox(view, node, mq)
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        else ->
            TextField("Unknown node")
    }
}

fun <Mes> updateBox(
    view: BoxView<Mes>,
    node: VBox,
    mq: BlockingQueue<Mes>
): VBox {
    val results = view.children.mapIndexed{ i, x ->
        updateNode(x, node.getChildren()[i], mq, node)
    }
    Platform.runLater {
        node.getChildren().setAll(results)
    }
    return node
}
