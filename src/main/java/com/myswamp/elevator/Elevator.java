package com.myswamp.elevator;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

enum State {
    IDLE, UP, DOWN
}

enum Direction {
    UPWARD, DOWNWARD
}
public class Elevator {
    private State state = State.IDLE;
    private int bottomFloor;
    private int topFloor;
    private int currentFloor;
    private BlockingQueue<Command> upwardQueue = new PriorityBlockingQueue<>(topFloor - bottomFloor, (c1, c2) -> {
        return (c1.getToFloor() - c1.getAtFloor()) - (c2.getToFloor() - c2.getAtFloor());
    });
    private BlockingQueue<Command> downwardQueue = new PriorityBlockingQueue<>(topFloor - bottomFloor, (c1, c2) -> {
        return (c1.getAtFloor() - c1.getToFloor()) - (c2.getAtFloor() - c2.getToFloor());
    });


    public void onReceiveCommand(Command command) {
        if(command instanceof OpenDoorCommand openDoorCommand) {
            onReceiveOpenDoorCommand(openDoorCommand);
        } else if(command instanceof MoveCommand moveCommand) {
            onReceiveMoveCommand(moveCommand);
        }
    }

    private void onReceiveOpenDoorCommand(OpenDoorCommand command) {
        if(command.getDirection() == Direction.UPWARD) {
            upwardQueue.offer(command);
        } else if(command.getDirection() == Direction.DOWNWARD) {
            downwardQueue.offer(command);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void onReceiveMoveCommand(MoveCommand command) {
        if(currentFloor > command.getToFloor()) {
            downwardQueue.offer(command);
        } else if(currentFloor < command.getToFloor()) {
            upwardQueue.offer(command);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void processCommands() throws InterruptedException {
        if(state == State.IDLE) {
            processDownwardsCommands();
            processUpwardCommands();
        } else if(state == State.UP) {
            processUpwardCommands();
        } else {
            processDownwardsCommands();
        }
        if (currentFloor == 0) {
            state = State.IDLE;
        } else if(currentFloor > 0) {
            state = State.DOWN;
        } else {
            state = State.UP;
        }
    }

    private void processDownwardsCommands() throws InterruptedException {
        state = State.DOWN;
        while (!downwardQueue.isEmpty()) {
            Command command = downwardQueue.poll();
            int toFloor = command.getToFloor();
            while(currentFloor != toFloor) {
                Thread.sleep(100);
                currentFloor -= 1;

                command = downwardQueue.poll();
                if(command != null) {
                    if(command.getToFloor() < toFloor) {
                        toFloor = command.getToFloor();
                    }
                }
            }
        }
    }

    private void processUpwardCommands() throws InterruptedException {
        state = State.UP;
        while (!upwardQueue.isEmpty()) {
            Command command = upwardQueue.poll();
            int toFloor = command.getToFloor();
            while(currentFloor != toFloor) {
                Thread.sleep(100);
                currentFloor += 1;

                command = upwardQueue.poll();
                if(command != null) {
                    if(command.getToFloor() > toFloor) {
                        toFloor = command.getToFloor();
                    }
                }
            }
        }
    }

}

abstract class Command {
    protected int atFloor;
    protected int toFloor;
    public int getAtFloor() {
        return atFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

}

class OpenDoorCommand extends Command {
    private Direction direction;
    OpenDoorCommand(int atFloor, Direction direction) {
        this.atFloor = atFloor;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}

class MoveCommand extends Command {
    MoveCommand(int atFloor, int toFloor) {
        this.atFloor = atFloor;
        this.toFloor = toFloor;
    }

}
