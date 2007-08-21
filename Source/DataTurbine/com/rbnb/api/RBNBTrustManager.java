/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.api;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


/**
 * RBNBTrustManager
 *
 * @author Eugen Kuleshov
 */
public class RBNBTrustManager implements X509TrustManager {

  public void checkClientTrusted( X509Certificate[] cert, String authType) {
  }

  public boolean isClientTrusted( X509Certificate[] cert) {
    return true;
  }

  public void checkServerTrusted( X509Certificate[] cert, String authType) {
  }

  public boolean isServerTrusted( X509Certificate[] cert) {
    return true;
  }

  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[ 0];
  }

}

