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

fun <Mes> makeNode(view: TextFieldView<Mes>, mq: BlockingQueue<Mes>): TextField {
    val tf = TextField(view.text)
    if (view.onInput != null) {
        tf.textProperty().addListener { _observable, _oldValue, newValue ->
            mq.offer(view.onInput!!(newValue))
        }
    }
    return tf
}

fun <Mes> makeNode(view: ButtonView<Mes>, mq: BlockingQueue<Mes>): Button {
    val button = Button(view.text)
    if (view.onClick != null) {
        button.setOnAction { _event ->
            mq.offer(view.onClick!!)
        }
    }
    return button
}

fun <Mes> makeNode(view: BoxView<Mes>, mq: BlockingQueue<Mes>): VBox {
    val vbox = VBox()
    val nodes = view.children.map{ makeNode(it, mq) }
    vbox.getChildren().addAll(nodes)
    return vbox
}

fun <Mes> makeNode(view: View<Mes>, mq: BlockingQueue<Mes>): Node {
    return when (view) {
        is TextFieldView ->
            makeNode(view, mq)
        is ButtonView ->
            makeNode(view, mq)
        is BoxView ->
            makeNode(view, mq)
    }
}

fun <Mes> replaceChild(
    view: View<Mes>,
    mq: BlockingQueue<Mes>,
    jfxParent: Pane
): Node {
    val child = makeNode(view, mq)
    Platform.runLater {
        jfxParent.getChildren().setAll(child)
        // jfxParent.getChildren().addAll(child)
    }
    return child
}

fun <Mes> updateNode(
    view: View<Mes>,
    node: Node,
    mq: BlockingQueue<Mes>,
    jfxParent: Pane
): Node {
    return when (node) {
        is TextField -> {
            when (view) {
                is TextFieldView -> {
                    Platform.runLater {
                        if (node.getText() != view.text) {
                            node.setText(view.text)
                        }
                        // FIXME: remove old listener and add the new one
                    }
                    node
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        }
        is Button -> {
            when (view) {
                is ButtonView -> {
                    Platform.runLater {
                        if (node.getText() != view.text) {
                            node.setText(view.text)
                        }
                        node.setOnAction(null) // disable action
                        if (view.onClick != null) {
                            node.setOnAction { _event ->
                                mq.offer(view.onClick!!)
                            }
                        }
                    }
                    node
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        }
        is VBox -> {
            when (view) {
                is BoxView ->
                    updateBox(view, node, mq)
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        }
        else -> {
            TextField("Unknown node")
        }
    }
}

fun <Mes> updateBox(
    view: BoxView<Mes>,
    node: VBox,
    mq: BlockingQueue<Mes>
): VBox {
    val results = mutableListOf<Node>()
    val minSize = minOf(view.children.size, node.getChildren().size)
    val maxSize = maxOf(view.children.size, node.getChildren().size)
    for (i in 0 until minSize) {
        results.add(updateNode(view.children[i], node.getChildren()[i], mq, node))
    }
    for (i in minSize until maxSize) {
        results.add(makeNode(view.children[i], mq))
    }
    Platform.runLater {
        node.getChildren().clear()
        node.getChildren().addAll(results)
    }
    return node
}
