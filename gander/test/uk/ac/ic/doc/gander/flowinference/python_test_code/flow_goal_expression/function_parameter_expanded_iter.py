def f(a, b):
    print a # not here
    print b # nor here

i = [1,2]
print i # blastoff

f(*i)