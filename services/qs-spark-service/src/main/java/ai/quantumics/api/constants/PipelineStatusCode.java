package ai.quantumics.api.constants;

public enum PipelineStatusCode {

	RUNNING(1), SUCCESS(2), FAILED(3);

	private int status;

	PipelineStatusCode(int status){
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public static String getEnumNameForValue(int value){
		for(PipelineStatusCode eachValue : PipelineStatusCode.values()) {
			if (eachValue.getStatus() == value) {
				return eachValue.name();
			}
		}
		return null;
	}
}
