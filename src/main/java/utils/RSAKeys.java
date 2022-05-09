package utils;

import java.math.BigInteger;
import java.security.interfaces.RSAKey;

public class RSAKeys implements RSAKey {

    BigInteger n, d, e;

    public RSAKeys(BigInteger n, BigInteger d, BigInteger e) {
        this.n = n;
        this.d = d;
        this.e = e;
    }

    public BigInteger getD() {
        return d;
    }

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub

        return super.equals(obj);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    @Override
    public BigInteger getModulus() {
        // TODO Auto-generated method stub
        return null;
    }

}
