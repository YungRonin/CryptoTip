package app.cryptotip.cryptotip.app.http;

import com.gani.lib.http.GHttp;

public class MyHttp extends GHttp {
  @Override
  protected String networkErrorMessage() {
    return "Failed";
  }

  @Override
  public String baseUrl() {
    return "";
  }
}
