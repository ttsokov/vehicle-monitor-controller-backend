package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class SetVehicleAutoRegulationActuatorStateException extends Exception {
	private Exception exception;

	public SetVehicleAutoRegulationActuatorStateException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while set vehicle isAutoRegulationActuatorState: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
