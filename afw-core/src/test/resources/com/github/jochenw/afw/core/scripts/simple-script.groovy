def StringWriter sw = new StringWriter();
def Appendable app = new Appendable() {
    Appendable append(CharSequence csq) {
        sw.append(csq);
        return this;
    }
    Appendable append(CharSequence csq, int start, int end) {
        sw.append(csq.subSequence(start, end));
        return this;
    }
    Appendable append(char c) {
        sw.append(c);
        return this;
    }
};
app.append("okay");
System.out.println(sw.toString());
return sw.toString();
