class A:
    def a(self):
        pass
    def b(self):
        pass
    def c(self):
        pass

class B:
    def a(self):
        pass
    def c(self):
        pass
    def d(self):
        pass
        
def main():
    if p:
        x = A()
    else:
        x = B()
        
    x.a()
    if p:
        x.b()
        x.c()
        x.b()
    x.c()
    if p:
        x.b()
