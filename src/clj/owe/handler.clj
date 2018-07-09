(ns owe.handler
  (:require
    [clj-http.client :as http]
    [compojure.core :refer [GET defroutes]]
    [compojure.route :refer [not-found resources]]
    [hiccup.page :refer [include-js include-css html5]]
    [hickory.core :refer [parse as-hickory]]
    [hickory.select :as s]
    [ring.util.http-response :as r]
    [owe.middleware :refer [wrap-middleware]]
    [config.core :refer [env]]))

;; -------------------------
;; Page

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:type        "text/css"
           :href        "https://unpkg.com/purecss@1.0.0/build/pure-min.css"
           :rel         "stylesheet"
           :integrity   "sha384-nn4HPE8lTHyVtfCBi5yW9d20FjT8BJwUXyWZT9InLYax14RDjBj46LmSztkmNP9w"
           :crossorigin "anonymous"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


;; -------------------------
;; Stats
(def endorsement-selectors
  {:shotcaller    "EndorsementIcon-border--shotcaller"
   :teammate      "EndorsementIcon-border--teammate"
   :sportsmanship "EndorsementIcon-border--sportsmanship"})

(defn ->int [s]
  (Integer/parseInt s))

(defn get-count
  [htree type]
  (-> (s/select
        (s/class (endorsement-selectors type))
        htree)
      first
      :attrs
      :data-value
      ->int))

(defn get-total
  [htree]
  (-> (s/select
        (s/class (endorsement-selectors :shotcaller))
        htree)
      first
      :attrs
      :data-total
      ->int))

(defn get-level
  [htree]
  (-> (s/select
        (s/child (s/class "endorsement-level")
                 (s/class "u-center"))
        htree)
      first
      :content
      first
      ->int))

(defn compute-stats
  [platform id]
  (try
    (let [htree (-> (http/get (str "https://playoverwatch.com/en-us/career/" platform "/" id)
                              {:cookie-policy :standard})
                    :body
                    (parse)
                    (as-hickory))]
      (r/ok {:level         (get-level htree)
             :shotcaller    (get-count htree :shotcaller)
             :teammate      (get-count htree :teammate)
             :sportsmanship (get-count htree :sportsmanship)
             :total         (get-total htree)}))
    (catch Throwable conn-ex
      (r/internal-server-error))))

;; -------------------------
;; Routes

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/api/:platform/:id" [platform id] (compute-stats platform id))

  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
