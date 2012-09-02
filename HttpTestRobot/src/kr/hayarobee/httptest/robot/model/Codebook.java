package kr.hayarobee.httptest.robot.model;


public class Codebook {
	
	public Codebook() {
	}

	public String read(String code, Robot robot) {
		String text = code;
		text = exchangeCode(text, "$ADMIN$", "admin");
		text = exchangeCode(text, "$ROBOT$", robot.getName());
		text = exchangeCode(text, "$TIMES$", robot.getCurrentJobRepeatCounter());
		text = exchangeCode(text, "$APP$", robot.getAppName());
		return text;
	}

	private String exchangeCode(String original, String code, String text) {
		if (original.equals(code)) return text;
		if (original.contains(code)) return original.replace(code, text);
		return original;
	}

}
