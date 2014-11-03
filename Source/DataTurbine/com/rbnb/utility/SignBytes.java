/*
Copyright 2014 Cycronix

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

// SignBytes - utility to add/check signature string added to bytearray (e.g. UDP packet)
// Matt Miller, Cycronix
// 10/15/2014

// Notes:
// this is a simplified not-super-secure example that uses a symetric key approach:
// 1) a password known to both parties is prepended to the data block then one-way hashed
// 2) the hash is prepended to data and sent (password itself never sent)
// 3) recipient rehashes password+data and confirms authenticity by comparing computed-hash with sent-hash
// improvement would be to use private/public key encryption of hash (vs entire data block)

package com.rbnb.utility;

import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;
import java.util.TreeSet;

//---------------------------------------------------------------------------------------------------------------------
public class SignBytes {
	
    int digestLength = 0;		// globals
	MessageDigest md = null;
	String password=null;
	boolean uniqueFlag;			// require unique entries (no duplicates)
	TreeSet hashTree = new TreeSet();

	// Sample main() to demo methods
    public static void main(String[] args) {
    	
        String digestType = "SHA-1";		// "MD5", "SHA-1", "SHA-256"

        if (args.length != 1) {
            System.out.println("Usage: SignBytes PW");
        }
        else try {
        	String pw = args[0];

            SignBytes sb = new SignBytes(digestType,pw,true);
            byte[] signed=null, putbytes=null;
            
            System.err.println("Loop on incrementing packets:");
            for(int i=0; i<5; i++) {
            	putbytes = ("TestString"+i).getBytes();
            	signed = sb.putSigned(putbytes);					// sign the byte array using password
            	checkResults(sb.getSigned(signed), putbytes);		// see if signature matches
            }
            
            System.err.println("Try another string (should be good):");
        	putbytes = ("AnotherString").getBytes();
            signed = sb.putSigned(putbytes);
        	checkResults(sb.getSigned(signed), putbytes);		

        	System.err.println("Try a duplicate entry:");
        	checkResults(sb.getSigned(signed), putbytes);
        	
            System.err.println("Try bad password:");
        	putbytes = ("AnotherString2").getBytes();
            signed = sb.putSigned(putbytes);
        	checkResults(sb.getSigned(signed, pw+"foo"), putbytes);		
            
            sb.checkMemory();
            int nput = 1000000;
        	System.err.println("try lots of put/gets ("+nput+") to test memory growth");
            for(int i=0; i<nput; i++) {
            	putbytes = ("NewString"+i).getBytes();
            	signed = sb.putSigned(putbytes);					// sign the byte array using password
            	sb.getSigned(signed);
//            	checkResults(sb.getSigned(signed), putbytes);		// see if signature matches
            }

            sb.checkMemory();
        } catch(Exception e) {
        	System.err.println("Exception: "+e);
        }
         
    };

    static void checkResults(byte[] getbytes, byte[] putbytes) {
		System.out.println("Check string: "+new String(putbytes));
    	if(getbytes != null) {
    		if(Arrays.equals(getbytes, putbytes)) 	System.out.println("Good Packet!");
    		else									System.out.println("data mismatch (program error?)");
    	}
    	else		System.out.println("Bad Packet :(");
    }
    
    void checkMemory() {
    	System.gc();
    	System.err.println("hashTree size: "+hashTree.size());
        Runtime runtime = Runtime.getRuntime();
        System.err.println("Used Memory: "+ (runtime.totalMemory() - runtime.freeMemory()) / (1024*1024)+" (MB)");
    }
    
//---------------------------------------------------------------------------------------------------------------------
// putSigned:  append digest (signature) to byte array using password (password is not stored)
    public byte[] putSigned(byte[] bytes) {
    	return putSigned(bytes, password);
    }
    
    public byte[] putSigned(byte[] bytes, String pw) {
    	byte[] signed = null;
    	try {
    		if(pw == null) throw new Exception("no password!");
    		byte[] combined = concat(pw.getBytes(), bytes); 	// concatenate pw with bytes
    		byte[] digest = md.digest(combined);
//    		System.err.println("digest: "+bytesToHex(digest)+", length: "+digest.length);
    		signed = concat(digest, bytes);						// concatenate digest with bytes
//    		System.err.println("signed: "+bytesToHex(signed));
    		
    	} catch(Exception e) {
    		System.err.println("signBytes Exception: "+e);
    		e.printStackTrace();
    	}
    	
    	return signed;
    }
    
//---------------------------------------------------------------------------------------------------------------------
// getSigned:  extract byte array and check digest vs password (returns null if bad match)
    
    public byte[] getSigned(byte[] signed) {
    	return getSigned(signed, password);
    }
    
    public byte[] getSigned(byte[] signed, String pw) {
    	byte[] bytes = null;
    	try {
    		byte[] digest = new byte[digestLength];
    		if(signed.length <= digestLength) {
    			throw new Exception("illegal signature (unsigned?)");
    		}
    		bytes = new byte[signed.length-digestLength];

    		System.arraycopy(signed, 0, digest, 0, digestLength);
    		if(uniqueFlag) {
    			if(!hashTree.add(ByteBuffer.wrap(digest))) {				// oops, duplicate, NG
    				System.err.println("getSigned bad packet, duplicate!");
    				return null;
    			}
//    			else System.err.println("getSigned, looks Unique!!, hashTree.size: "+hashTree.size());
    		}
    		System.arraycopy(signed, digestLength, bytes, 0, bytes.length);
    	
    		byte[] trysigned = putSigned(bytes, pw);
    		if(!Arrays.equals(trysigned, signed)) {
    			System.err.println("getSigned bad packet, password mismatch!");
    			return null;				// mark as NG
    		}
    	} catch(Exception e) { 
    		System.err.println("readSigned Exception: "+e);
    		e.printStackTrace();
    	}

//    	System.err.println("getSigned looks GOOD! bytes.length:  "+bytes.length);
		return bytes;
    }

//---------------------------------------------------------------------------------------------------------------------
    // concat:  utility function to concatentate two arrays
    byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return (c);
    }
    
    // bytesToHex:  utility function to print byte array as Hex (for debugging)
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
//---------------------------------------------------------------------------------------------------------------------
// SignBytes constructor:  establish Hash digest type and length
    
    public SignBytes(String digestType, String ipw) {
    	try {
    		password = ipw;
    		md = MessageDigest.getInstance(digestType);
    		digestLength = md.getDigestLength();
    		uniqueFlag = false;
    	} catch(Exception e) {
    		System.err.println("SignBytes Exception: "+e);
    	}
    }
    
    public SignBytes(String digestType, String ipw, boolean unique) {
    	try {
    		password = ipw;
    		md = MessageDigest.getInstance(digestType);
    		digestLength = md.getDigestLength();
    		uniqueFlag = unique;
    	} catch(Exception e) {
    		System.err.println("SignBytes Exception: "+e);
    	}
    }
    
}



