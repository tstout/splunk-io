(ns splunk-io
  (:import (com.splunk Service SSLSecurityProtocol)
           (java.util HashMap)))

(defn connect
  "Connect to splunk using the Java SDK.
  The option map should contain :username and :password and optionally, :host.
  Returns a splunk Service instance."
  [opts]
  (let [{:keys [username password host] :or {host "splunk.containerstore.com"}} opts]
    (Service/setSslSecurityProtocol SSLSecurityProtocol/TLSv1_2)
    (Service/connect (HashMap.
                       {"host"     host
                        "username" username
                        "password" password
                        "scheme"   "https"}))))



