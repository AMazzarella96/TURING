import java.io.Serializable;
import java.util.ArrayList;

public class Request implements Serializable {
    private String request;
    private ArrayList<String> param;
    private static final long serialVersionUID = 1L;

    public Request(String request, ArrayList<String> param){
        this.request = request;
        this.param = param;
    }

    public Request(String request){
        this.request=request;
    }

    public String getRequest(){
        return request;
    }

    public ArrayList<String> getParam(){
        return param;
    }

}
