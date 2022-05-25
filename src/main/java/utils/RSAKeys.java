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

    public void setN(BigInteger n) {
        this.n = n;
    }

    public void setD(BigInteger d) {
        this.d = d;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public BigInteger getModulus() {
        return null;
    }

    public BigInteger getN() {
        return n;
    }

}
