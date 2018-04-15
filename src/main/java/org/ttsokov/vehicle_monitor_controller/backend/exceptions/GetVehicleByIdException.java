package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class GetVehicleByIdException extends Exception {
	private Exception exception;

	public GetVehicleByIdException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while get vehicle by id: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
