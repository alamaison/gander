def f(a, b):
    print a # not here
    print b # nor here

m = { "b": 1, "a" : 2 }
print m # blastoff

f(**m)