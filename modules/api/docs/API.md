<h1>API Documentation<H1>

This document contains the documentation of the API that is consumed by the mobile SDKs. 

<h2>Authentication functionalities</h2>

This section describes the authentication functionalities of Wazza's mobile API.

**Authentication**
----
  Performs token based authentication. 
  Additionally, an optional header can be sent if the user exists or not. It is up to the sender to store that information, which can be done analysing the result of API call. The goal of this is to reduce the number of database requests for user existence. A boolean result is sent if a mobile user was created or not.

* **URL**

  `/api/auth/`

* **Method:**

  `POST`
   
*  **Headers**
    
    `SDK-TOKEN`

    `X-UserExists` *optional*

* **Success Response:**

  * **Code:** 200 <br />
    **Content:**
  ```json
    {
      "result": "[boolean]"
    }
  ```

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{}`


<h2>Sessions</h2>

+ Save Session

**Save session**
----
  Saves information regarding one or more sessions.

* **URL**

  /api/session/new/

* **Method:**

  `POST`
   
*  **Headers**
    
    `SDK-TOKEN`

* **Data Params**

  ```json
    {
      "userId": "[string]",
      "applicationName": "[string]",
      "companyName": "[string]",
      "startTime": "[string]",
      "endTime": "[string]",
      "deviceInfo": {
        "osName": "[string]",
        "osVersion": "[string]",
        "deviceModel": "[string]"
      },
      "location": "#optional"{
        "latitude": "[double]",
        "longitude": "[double]"
      },
      "purchases":[
        {
          "[string]"
        },
        "..."
      ]
    }
  ```

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{}`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{}`


<h2>Payments</h2>

+ Save payment
+ PayPal payment verification

  **Save payment**
----
  Saves information regarding one payment.

* **URL**

  /api/purchase/

* **Method:**

  `POST`

*  **Headers**
    
    `SDK-TOKEN`

* **Data Params**
 
Depending on the payment system, the json structure will change. However there's a common structure for all payment information.

+ Common payment structure:

  ```json
    {
      "paymentSystem": "[int]",
      "id": "[string]",
      "itemId": "[string]",
      "userId": "[string]",
      "price": "[double]",
      "time": "[Date]",
      "deviceInfo": {
        "osName": "[string]",
        "osVersion": "[string]",
        "deviceModel": "[string]"
      },
      "sessionId": "[string]",
      "success": "[boolean]"
    }
  ```
  
 + PayPal payment
  
 
    If the payment was made via PayPal, the following attributes are added to common structure:
  
    ```json
    {
      "currencyCode": "[string]",
      "shortDescription": "[string]",
      "intent": "[string]",
      "processable": "[boolean]",
      "responseID": "[string]",
      "state": "[string]",
      "responseType": "[string]",
      "quantity": "[int]"
    }
  ```
  
  + In-app Purchase payment
  

    In-app Purchase payments have no extra attributes.

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{}`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** 
   
    ```json
    {
      "error": "[string]"
    }
  ```

  **PayPal payment verification**
----
  Sends a PayPal payment for verification.

* **URL**

  /payment/verify/

* **Method:**

  `POST`

*  **Headers**
    
    `SDK-TOKEN`

* **Data Params**

  ```json
    {
      "responseID": "[string]",
      "price": "[double]",
      "currencyCode": "[string]"
    }
  ```

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{}`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** 
   
    ```json
    {
      "error": "[string]"
    }
  ```
