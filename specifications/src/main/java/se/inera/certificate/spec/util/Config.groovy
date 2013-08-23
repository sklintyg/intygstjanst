package se.inera.certificate.spec.util

class Config {

	String property
	String value
	
	void execute() {
		if (value && !value.startsWith("undefined variable:")) System.setProperty(property, value)
	}
}
