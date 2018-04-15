package org.ttsokov.vehicle_monitor_controller.backend.exceptions;

public class DeleteVehicleException extends Exception {
	private Exception exception;

	public DeleteVehicleException(Exception e) {
		this.exception = e;
	}

	@Override
	public String getMessage() {
		String msg = "Error while deleting vehicle: ";

		if (this.exception != null) {
			msg += this.exception.getMessage();
		}

		return msg;
	}
}
