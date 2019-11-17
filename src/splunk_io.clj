(ns splunk-io
  (:import (com.splunk Service SSLSecurityProtocol Args ResultsReaderJson JobResultsArgs$OutputMode JobEventsArgs$OutputMode)
           (java.util HashMap)
           (java.time LocalDateTime))
  (:require [clojure.data.json :as json]))

;;
;; API examples:
;; https://dev.splunk.com/enterprise/docs/java/sdk-java/howtousesdkjava/howtoworkjobjava/
;; https://dev.splunk.com/enterprise/docs/java/sdk-java/howtousesdkjava/howtodisplaysearchsdkjava/
;;

(defn connect
  "Connect to splunk.
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
  (->
    (Args.)
    (.add "earliest_time" (-> (LocalDateTime/now) (.minusDays 1) str))
    (.add "latest_time" (-> (LocalDateTime/now) str))
    (.add "output_mode" JobResultsArgs$OutputMode/JSON)))

(defmulti msg-parse (fn [msg] (-> msg :sourcetype keyword)))

(defmethod msg-parse :_json [msg]
  (->
    msg
    :_raw
    (json/read-str :key-fn keyword)))

(defmethod msg-parse :default [msg]
  (-> msg :_raw))

(defn filter-result [logs]
  (map
    (fn [log]
      (let [{:keys [sourcetype _raw source _time]} log]
        {:sourcetype sourcetype
         :msg    (msg-parse log)
         :source source
         :time   _time}))
    logs))

(defn filter-result-dbg [logs]
  logs)

(defn process-result
  "Given an input stream read the contents and convert it from json to EDN."
  [is]
  (let [logs (->
               is
               slurp
               (json/read-str :key-fn keyword)
               :results)]
    (filter-result logs)))


; TODO - add source to param map

(defn search!
  "Performs a blocking one-shot search. The supplied opts map is expected to
  contain splunk credentials."
  [opts]
  (->
    (connect opts)
    ;;(.oneshotSearch "search index=lab | head 10" (search-args))
    (.oneshotSearch "search source=*space-services* | head 200" (search-args))
    process-result))


(comment
  (search! {:username "user" :password "pwd"})

  (filter
  #(clojure.string/includes? (:msg %) "ERROR")
  (search! {:username "btstout" :password "pwd"}))


  )

