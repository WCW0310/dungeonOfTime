package core

import core.GameKernel

abstract class Scene : GameKernel.GameInterface {

    abstract fun sceneBegin()

    abstract fun sceneEnd()

}