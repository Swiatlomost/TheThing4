use jni::objects::{JByteArray, JClass};
use jni::sys::{jboolean, jbyteArray};
use jni::JNIEnv;
use sha2::{Digest, Sha256};
use std::ptr;

fn empty_byte_array(env: &mut JNIEnv) -> jbyteArray {
    env.new_byte_array(0)
        .map(|array| array.into_raw())
        .unwrap_or_else(|_| ptr::null_mut())
}

#[no_mangle]
pub extern "system" fn Java_com_thething_cos_lightledger_LightLedgerNative_isAvailable(
    env: JNIEnv,
    _class: JClass,
) -> jboolean {
    env.exception_clear().ok();
    1
}

#[no_mangle]
pub extern "system" fn Java_com_thething_cos_lightledger_LightLedgerNative_hashFingerprintPayload(
    mut env: JNIEnv,
    _class: JClass,
    payload: JByteArray,
) -> jbyteArray {
    match env.convert_byte_array(payload) {
        Ok(bytes) => {
            let digest = Sha256::digest(&bytes);
            match env.byte_array_from_slice(&digest) {
                Ok(array) => array.into_raw(),
                Err(_) => empty_byte_array(&mut env),
            }
        }
        Err(_) => empty_byte_array(&mut env),
    }
}
