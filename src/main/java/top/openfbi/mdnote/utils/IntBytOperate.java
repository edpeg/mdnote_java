package top.openfbi.mdnote.utils;

public class IntBytOperate {

    // 取出n的第m位
    public static int getBit ( int n,  int m ) {
        return  (n  >>  (m - 1 ) )  &  1 ;
    }

    //从低位到高位.将n的第m位置1
    public static int setBitToOne ( int n,  int m ) {
        return n  |  ( 1 << (m - 1 ) ) ;
    }

    // 从低位到高位,将n的第m位置0
    public static int setBitToZero ( int n,  int m ) {
        return n  & ~ ( 1 << (m - 1 ) ) ;
    }
}
