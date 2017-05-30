package javaapplication1;

public enum Protocol {

    T0("T=0"), T1("T=1"), TCL("T=CL"), AUTO("*");

    private final String name;

    private Protocol(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
