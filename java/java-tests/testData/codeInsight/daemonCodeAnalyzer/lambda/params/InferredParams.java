interface I {
  void m(int i);
}

interface A<B> {
  void foo(B b);
}

class Foo {
  I ii = (final <error descr="Cannot resolve symbol 'k'">k</error><error descr="Identifier expected">)</error>->{};
  I ii1 = (k)->{
    int i = k;
    <error descr="Incompatible types. Found: 'int', required: 'java.lang.String'">String s = k;</error>
  };
  
  A<String> a = (ab) -> {
    String s = ab;
    <error descr="Incompatible types. Found: 'java.lang.String', required: 'int'">int i = ab;</error>
  };

  A<Integer> bazz() {
    bar((o) -> {
      String s = o;
      <error descr="Incompatible types. Found: 'java.lang.String', required: 'int'">int i = o;</error>
    });
    return (i) -> {
      Integer k = i;
      int n = i;
      <error descr="Incompatible types. Found: 'java.lang.Integer', required: 'java.lang.String'">String s = i;</error>
    };
  }

  void bar(A<String> a){}
}
