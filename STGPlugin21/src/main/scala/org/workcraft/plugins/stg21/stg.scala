package org.workcraft.plugins.stg21

sealed trait Node

sealed class Arc extends Node
sealed class Component extends Node

class Place extends Component
class SignalTransition extends Component
class DummyTransition extends Component
