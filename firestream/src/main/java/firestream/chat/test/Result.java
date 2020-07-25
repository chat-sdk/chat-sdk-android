package firestream.chat.test;

public class Result {

    public String errorMessage;
    public Test test;

    public Result(Test test, String errorMessage) {
        this.test = test;
        this.errorMessage = errorMessage;
    }

    public static Result success(Test test) {
        return new Result(test, null);
    }

    public static Result failure(Test test, String message) {
        return new Result(test, message);
    }

    public boolean isSuccess() {
        return errorMessage == null;
    }

}
