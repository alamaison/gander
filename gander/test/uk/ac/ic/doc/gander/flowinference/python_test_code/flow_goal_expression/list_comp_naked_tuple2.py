# The comma delimited list doesn't appear in the AST as a tuple (unlike the
# real for-loop). "Hello" appears as the iterator and x appears as an
# if statement!
x = 42
print x # blastoff

t = [i for i in "Hello", x]

print t
print t[0]
print t[1]
