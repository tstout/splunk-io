(ns splunk-io
  (:import (com.splunk Service SSLSecurityProtocol Args ResultsReaderJson JobResultsArgs$OutputMode JobEventsArgs$OutputMode)
           (java.util HashMap))
  (:require [clojure.data.json :as json]))

;;
;; API examples:
;; https://dev.splunk.com/enterprise/docs/java/sdk-java/howtousesdkjava/howtoworkjobjava/
;; https://dev.splunk.com/enterprise/docs/java/sdk-java/howtousesdkjava/howtodisplaysearchsdkjava/
;;

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

(defn search-args []
  ;; Args oneshotSearchArgs = new Args();
  ;;oneshotSearchArgs.put("earliest_time", "2012-06-19T12:00:00.000-07:00");
  ;;oneshotSearchArgs.put("latest_time",   "2012-06-20T12:00:00.000-07:00");)
  (->
    (Args.)
    (.add "earliest_time" "2019-10-11T12:00:00.000-07:00")
    (.add "latest_time" "2019-11-19T12:00:00.000-07:00")
    (.add "output_mode" JobResultsArgs$OutputMode/JSON)))

(defn process-result
  "Given an input stream slurp the contents and convert it from json to EDN."
  [is]
  (->
    is
    slurp
    (json/read-str :key-fn keyword)))

(defn search!
  "Performs a blocking one-shot search. The supplied opts map is expected to
  contain splunk credentials."
  [opts]
  (->
    (connect opts)
    ;;(.oneshotSearch "search index=lab | head 10" (search-args))
    (.oneshotSearch "search source=*space-design-services*| head 10" (search-args))
    process-result))





