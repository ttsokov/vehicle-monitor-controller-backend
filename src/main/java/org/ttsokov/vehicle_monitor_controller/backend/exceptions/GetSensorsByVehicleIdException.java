package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class GetSensorsByVehicleIdException extends Exception {
	private static final long serialVersionUID = 1L;

	private Exception exception;

	public GetSensorsByVehicleIdException(Exception e) {
		exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while getting sensor by id: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
