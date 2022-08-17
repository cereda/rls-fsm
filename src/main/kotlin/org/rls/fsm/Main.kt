package org.rls.fsm

import com.tinder.StateMachine

sealed class State {
    object S0 : State()
    object S1 : State()
    object S2 : State()
    object S3 : State()
    object S4 : State()
    object S5 : State()
    object S5p1 : State()
    object S5p2 : State()
    object E0 : State()
    object E1 : State()
    object E2 : State()
    object E3 : State()
    object E4 : State()
    object A0 : State()
    object A0p0 : State()
    object A1 : State()
    object A1p0 : State()
    object A2 : State()
    object A3 : State()
    object R0 : State()
    object R1 : State()
}

sealed class Event {
    object E0 : Event()
    object E1 : Event()
    object E2 : Event()
    object E3 : Event()
    object E4 : Event()
    object E5 : Event()
    object E6 : Event()
    object E7 : Event()
    object E8 : Event()
    object E9 : Event()
    object E10 : Event()
    object E11 : Event()
    object E12 : Event()
    object E13 : Event()
    object E14 : Event()
    object E15 : Event()
    object E16 : Event()
    object E17 : Event()
    object T0 : Event()
    object T1 : Event()
    object T2 : Event()
    object T3 : Event()
    object R0 : Event()
    object R1 : Event()
}

sealed class SideEffect {
    object NopEffect : SideEffect()
}

val groups = mapOf(
    1 to setOf(
        State.S0, State.S1, State.S2, State.S3, State.S4, State.S5, State.S5p1,
        State.S5p2, State.E0, State.E1, State.E2, State.E3, State.E4
    ),
    2 to setOf(
        State.A0, State.A0p0
    ),
    3 to setOf(
        State.R0
    ),
    4 to setOf(
        State.R1
    ),
    5 to setOf(
        State.A1, State.A1p0
    ),
    6 to setOf(
        State.A2
    ),
    7 to setOf(
        State.A3
    )
)

val transitions = mapOf(
    (State.S0 to Event.E0) to State.S1,
    (State.S1 to Event.E1) to State.E0,
    (State.S1 to Event.E4) to State.S2,
    (State.S2 to Event.R0) to State.R0,
    (State.S2 to Event.E5) to State.S3,
    (State.S3 to Event.E6) to State.E2,
    (State.S3 to Event.E7) to State.S4,
    (State.S4 to Event.E8) to State.E1,
    (State.S4 to Event.E11) to State.S5,
    (State.S5 to Event.E12) to State.S5p1,
    (State.S5 to Event.E15) to State.S5p2,
    (State.S5 to Event.R1) to State.R1,
    (State.S5p1 to Event.E13) to State.E3,
    (State.S5p2 to Event.E16) to State.E4,
    (State.E0 to Event.T0) to State.A0,
    (State.A0 to Event.E2) to State.A0p0,
    (State.A0p0 to Event.E3) to State.S1,
    (State.E1 to Event.T1) to State.A1,
    (State.A1 to Event.E9) to State.A1p0,
    (State.A1p0 to Event.E10) to State.S4,
    (State.S5p1 to Event.E13) to State.E3,
    (State.E3 to Event.T2) to State.A2,
    (State.A2 to Event.E14) to State.S5,
    (State.S5p2 to Event.E16) to State.E4,
    (State.E4 to Event.T3) to State.A3,
    (State.A3 to Event.E17) to State.S5,
)

val activeGroups = mutableSetOf(1)

fun build(): StateMachine<State, Event, SideEffect> {
    return generateMachine(State.S0)
}

fun step(
    currentMachine: StateMachine<State, Event, SideEffect>,
    currentEvent: Event
): StateMachine<State, Event, SideEffect> {

    val currentState = currentMachine.state

    activeGroups.add(transitions[currentState to currentEvent]?.let {
        groups.filter { p -> it in p.value }.keys.firstOrNull() ?: 1
    } ?: 1)

    return generateMachine(currentState)
}

private fun generateMachine(currentState: State): StateMachine<State, Event, SideEffect> {
    val states = activeGroups.flatMap { groups[it]!! }

    val machine = StateMachine.create<State, Event, SideEffect> {
        initialState(currentState)

        states.forEach { s ->
            state(s) {
                transitions.filter { k -> k.key.first == s }
                    .forEach { (k, v) -> on(k.second) { transitionTo(v, SideEffect.NopEffect) } }
            }
        }
    }
    return machine
}

fun walk(
    currentMachine: StateMachine<State, Event, SideEffect>,
    currentEvent: Event
): StateMachine<State, Event, SideEffect> {
    val machine = step(currentMachine, currentEvent)
    machine.transition(currentEvent)
    return machine
}

fun main() {
    val events = listOf(Event.E0, Event.E1, Event.T0, Event.E2, Event.E3, Event.E4, Event.R0)

    val fsm = events.fold(build()) { x, y -> walk(x, y) }

    if (fsm.state in setOf(State.R0, State.R1)) {
        println("Execution reached a satisfied requirement state.")
    } else {
        println("Something wrong happened.")
    }
}
