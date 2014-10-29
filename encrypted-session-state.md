Encryption of client-side session state
=======================================

Client-side session state is implemented as a set of cookies. A valid non-empty session will contain:

-   One `tracking ID` cookie.
-   One `session secret key suffix` cookie.
-   Zero-to-many `application data` cookies.

The concatenated payloads from the `tracking ID` and `session secret key suffix` cookies form the `session secret key` 
cipher text (with IV). This is decrypted using the `application secret key` to produce a `session secret key` that is 
used to encrypt/decrypt payloads held by all `application data` cookies.

Session metadata
----------------

Cookie `key -> value` pairs:

    "tracking_id"                    -> Substring(SessionSecretKeyCipherTextWithIV, 0, 20)
    Hash(SessionSecretKeySuffixKey)  -> Substring(SessionSecretKeyCipherTextWithIV, 20, *)
    
Functions:
    
    SessionSecretKeySuffixKey        = SessionSecretKeyID
    SessionSecretKeyCipherTextWithIV = [IV, SessionSecretKeyCipherText]
    SessionSecretKeyCipherText       = SymmetricEncryption(IV, ApplicationSecretKey, SessionSecretKeySignedClearText)
    SessionSecretKeySignedClearText  = [SessionSecretKeyID, SessionSecretKey]
    SessionSecretKey                 = SecureRandom128Bit()
    SessionSecretKeyID               = "FE291934-66BD-4500-B27F-517C7D77F26B" // Default constant. Override stored in application config.

Session application data
------------------------

Cookie `key -> value` pair for each piece of `application data`:

    CookieName -> CookieValueCipherTextWithIV

Functions:
    
    CookieName                  = Hash([SessionSecretKey, CookieKey])
    CookieValueCipherTextWithIV = [IV, CookieValueCipherText]
    CookieValueCipherText       = SymmetricEncryption(IV, SessionSecretKey, CookieValueSignedClearText)
    CookieValueSignedClearText  = [CookieKey, Payload]
    CookieKey                   = <Clear text that uniquely identifies this piece of application data, defined by caller>
    Payload                     = <Clear text or serialized object, defined by caller>
    SessionSecretKey            = <Extracted from session metadata>

Common functions
----------------

    Hash                  = SHA-1(<clear text>)
    ApplicationSecretKey  = SecureRandom128Bit() // Generated once and stored in application config
    IV                    = SecureRandom128Bit()
    SymmetricEncryption   = AES-128(<initialization vector>, <secret key>, <clear text>)
