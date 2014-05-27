package se.inera.certificate

class AnonymizeString {
    
    static String anonymize(String s) {
        s.replaceAll('[^\'"()\\{\\}\\[\\]\\s0-9]','x').replaceAll('[0-9]','9')
    }
}
