package com.myswamp.elevator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ElevatorTest {
    private Elevator elevator;

    @BeforeAll
    public void startElevator() {
        elevator = new Elevator(-2, 18);
        new Thread(() -> {
            try {
                elevator.start();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Test
    public void testElevatorStarted() throws InterruptedException {
        Thread.sleep(100);
        assertTrue(elevator.getState() == State.IDLE);

    }

    @Test
    public void testGoUp() throws InterruptedException {
        elevator.issueCommand(new OpenDoorCommand(elevator.getCurrentFloor(), 10, Direction.UPWARD));
        Thread.sleep(1100);
        assertEquals(10, elevator.getCurrentFloor());
    }
}