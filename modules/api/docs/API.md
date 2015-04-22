<h1>API Documentation<H1>


TODO: description bla bla bla

<h2>Sessions</h2>

+ Save Session

**Save session**
----
  Saves information regarding one or more sessions.

* **URL**

  /api/session/new/:companyName/:applicationName

* **Method:**

  `POST`

*  **URL Params**

   **Required:**

   `companyName=[string]`

   `applicationName=[string]`
   
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
  Sends a PayPal payment 

* **URL**

  /payment/verify/

* **Method:**

  `POST`

*  **Headers**
    
    `SDK-TOKEN`

* **Data Params**

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
