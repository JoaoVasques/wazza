<h1>API Documentation<H1>


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

* **Data Params**

  ```json
    {
      userId: [string],
      applicationName: [string],
      companyName: [string],
      startTime: [string],
      endTime: [string],
      deviceInfo: {
        osName: [string],
        osVersion: [string],
        deviceModel: [string]
      },
      location: #optional{
        latitude: [double],
        longitude: [double]
      }
    }
  ```

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** `{}`

* **Error Response:**

  * **Code:** 404 NOT FOUND <br />
    **Content:** `{}`



* **Sample Call:**

  **TODO**

  ```javascript
    $.ajax({
      url: "/users/1",
      dataType: "json",
      type : "GET",
      success : function(r) {
        console.log(r);
      }
    });
  ```
