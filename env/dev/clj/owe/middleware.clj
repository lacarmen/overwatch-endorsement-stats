(ns owe.middleware
  (:require
    [prone.middleware :refer [wrap-exceptions]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
    [ring.middleware.json-response :refer [wrap-json-response]]
    [ring.middleware.reload :refer [wrap-reload]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults site-defaults)
      wrap-json-response
      wrap-exceptions
      wrap-reload))
