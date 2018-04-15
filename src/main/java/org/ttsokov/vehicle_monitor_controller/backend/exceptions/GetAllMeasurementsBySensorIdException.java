package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class GetAllMeasurementsBySensorIdException extends Exception {
	private Exception exception;

	public GetAllMeasurementsBySensorIdException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while getting measurements by sensor id: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
