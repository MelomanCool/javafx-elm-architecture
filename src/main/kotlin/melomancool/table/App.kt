package melomancool.table

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

import melomancool.table.jfx.makeNode
import melomancool.table.jfx.updateNode


data class Model(val counter: Int)

sealed class Msg
object Increment: Msg()
object Decrement: Msg()
object Reset: Msg()


val initialModel = Model(0)

fun update(msg: Msg, model: Model): Model {
    return when (msg) {
        Increment ->
            Model(model.counter + 1)
        Decrement ->
            Model(model.counter - 1)
        Reset ->
            Model(0)
    }
}

fun view(model: Model): View<Msg> =
    if (model.counter != 5) {
        BoxView<Msg>(listOf(
            ButtonView("+", onClick = Increment),
            TextFieldView<Msg>(model.counter.toString()),
            ButtonView("-", onClick = Decrement)
        ))
    } else {
        ButtonView("Huh?", onClick = Reset)
    }

class TableViewSample : Application() {
    override fun start(primaryStage: Stage) {
        val mq = LinkedBlockingQueue<Msg>()
        var node = makeNode(view(initialModel), mq)

        val root = javafx.scene.layout.VBox()
        root.getChildren().addAll(node)
        
        thread(start = true, isDaemon = true) {
            var model = initialModel
            while (true) {
                val msg = mq.take()
                model = update(msg, model)
                node = updateNode(view(model), node, mq, root)
                println(model)
            }
        }

        primaryStage.setScene(Scene(root, 100.0, 100.0))
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(TableViewSample::class.java, *args)
}
