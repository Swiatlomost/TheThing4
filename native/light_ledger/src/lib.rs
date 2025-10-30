use jni::objects::{JByteArray, JClass};
use jni::sys::{jboolean, jbyteArray};
use jni::JNIEnv;
use sha2::{Digest, Sha256};

#[no_mangle]
pub extern "system" fn Java_com_example_cos_lightledger_LightLedgerNative_isAvailable(
    env: JNIEnv,
    _class: JClass,
) -> jboolean {
    env.exception_clear().ok();
    1
}

#[no_mangle]
pub extern "system" fn Java_com_example_cos_lightledger_LightLedgerNative_hashFingerprintPayload(
    mut env: JNIEnv,
    _class: JClass,
    payload: JByteArray,
) -> jbyteArray {
    match env.convert_byte_array(payload) {
        Ok(bytes) => {
            let digest = Sha256::digest(&bytes);
            env.byte_array_from_slice(&digest).unwrap_or_else(|_| env.new_byte_array(0).unwrap())
        }
        Err(_) => env.new_byte_array(0).unwrap(),
    }
}
