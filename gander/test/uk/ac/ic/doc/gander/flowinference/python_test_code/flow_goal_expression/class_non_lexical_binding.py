# variables in a class aren't actually lexically bound which is quite nasty

x = 42
print x # blastoff
y = []

class A:
    x = x

print A.x # the global ends up in A's namespace

class B:
    pass

print B.x # global doesn't get into a class namespace unless it was assigned