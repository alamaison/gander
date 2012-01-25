#_
#__debug__
#__doc__
#__import__
#__name__
#__package__
import types

def abs(o):
    pass #return o.__abs__()

def all(iterable):
    for element in iterable:
        if not element:
            return False
    return True

def any(iterable):
    for element in iterable:
        if element:
            return True
    return False

def apply():
    pass

def bin(x):
    return ""

def bool(x=False):
    if x:
        return True
    else:
        return False

def buffer():
    pass

def bytearray():
    pass

def bytes():
    pass

def callable(object):
    return True

def chr(i):
    pass

def classmethod():
    pass

def cmp(lhs, rhs):
    pass #lhs.__eq__(rhs)

def coerce():
    pass

def compile():
    pass

def complex():
    pass

def copyright():
    pass

def credits():
    pass

def delattr():
    pass

def dir():
    pass

def divmod():
    pass

def enumerate():
    pass

def eval():
    pass

def execfile():
    pass

def exit():
    pass

def file():
    pass

def filter():
    pass

class float(type):
    pass

def format():
    pass

def frozenset():
    pass

def getattr():
    pass

def globals():
    pass

def hasattr():
    pass

def hash(object):
    return 42;

def help():
    pass

def hex(x):
    pass

def id(object):
    pass

def input():
    pass

class int(type):
    pass

def intern():
    pass

def isinstance(object, classinfo):
    return True

def issubclass(klass, classinfo):
    return True

def iter():
    pass

def len(iterable):
    return iterable.__len__()

def license():
    pass

def locals():
    pass

class long(type):
    pass

def map():
    pass

def max():
    pass

def min():
    pass

def next():
    pass

def oct():
    pass

def open():
    pass

def ord():
    pass

def pow():
    pass

#def print():
#    pass

def property():
    pass

def quit():
    pass

def range():
    pass

def raw_input():
    pass

def reduce():
    pass

def reload():
    pass

def repr():
    pass

def reversed():
    pass

def round():
    pass

def setattr():
    pass

def slice():
    pass

def sorted():
    pass

def staticmethod():
    pass

def sum():
    pass

def super():
    pass

class tuple(type):
    pass

class type:
    pass

def unichr():
    pass

def vars():
    pass

def xrange():
    pass

def zip():
    pass

class object:
    
    def __init__(obj, *args, **keywords):
        pass
    
    __class__ = object
    
    def __delattr__(self, name):
        #del self.name
        pass
    
    __dict__ = {}
    __doc__ = "Most base class"
    
    def __format__(self, *args, **keywords):# no in pypy
        pass
    
    def __getattribute__(self, name):
        return self.__dict__[name]
    
    def __hash__(self):
        return hash(self)
    
    __module__ = "__builtin__"
    
    def __new__(type, *args, **keywords):
        pass
    
    def __reduce__(self, proto=0):
        pass
    
    def __reduce_ex__(self, proto=0):
        pass
    
    def __repr__(self):
        return repr(self)
    
    def __setattr__(self, name, value):
        self.__dict__[name] = value
    
    def __sizeof__(self): # not in pypy
        return 42
    
    def __str__(self):
        return str(self)
    
    # __subclasshook__ not in pypy
    
    __weakref__ = None

class BaseException(object):
    
    def __getitem__(self, i):
        return self.data[i]
    
    def __getslice__(self, i, j): # not in pypy
        pass
    
    def __setstate__(self, dict):
        pass
    
    def __unicode__(self, *args, **keywords): #not in pypy
        pass
    
    args = ()
    message = ""

class Exception(BaseException):
    pass

class basestring(object):
    pass

class str(basestring):
    def __add__(self, x):
        self.data = self.data + x
    
    def __contains__(self, item):
        return item in self.data
    
    def __eq__(self, other):
        return self.data == other.data
    
    def __ge__(self, other):
        return self.data >= other.data
    
    def __gt__(self, other):
        return self.data > other.data
    
    def __le__(self, other):
        return self.data <= other.data
    
    def __lt__(self, other):
        return self.data < other.data
    
    def __getitem__(self, i):
        return self.data[i]
    
    def __getnewargs__(self, *args, **keywords):
        pass
    
    def __getslice__(self, i, j):
        return self.data[i:j]
    
    def __len__(self):
        return len(self.data)
    
    def __mod__(self, x):
        return self.data % x
    
    def __mul__(self, x):
        return self.data * x
    
    def __ne__(self, other):
        return self.data != other.data
    
    def __rmod__(self, x):
        return x % self.data
    
    def __rmul__(self, x):
        return x * self.data
    
    def capitalize(self):
        pass
    
    def center(self, width, fillchar=None):
        pass
    
    def count(self, sub=None, start=None, end=None):
        pass
    
    def decode(self, encoding=None, errors=None):
        pass
    
    def encode(self, encoding=None, errors=None):
        pass
    
    def endswith(self, suffix, start=None, end=None):
        pass
    
    def expandtabs(self, tabsize=None):
        pass
    
    def find(self, sub, start=None, end=None):
        pass
    
    def format(self, *args, **kwargs):
        pass
    
    def index(self, sub, start=None, end=None):
        pass
    
    def isalnum(self):
        pass
    
    def isalpha(self):
        pass
    
    def isdigit(self):
        pass
    
    def islower(self):
        pass
    
    def isspace(self):
        pass
    
    def istitle(self):
        pass
    
    def isupper(self):
        pass
    
    def join(self, iterable):
        pass
    
    def ljust(self, width, fillchar=None):
        pass
    
    def lower(self):
        pass
    
    def lstrip(self, chars=None):
        pass
    
    def partition(self, sep):
        pass
    
    def replace(self, old, new, count=None):
        pass
    
    def rfind(self, sub, start=None, end=None):
        pass
    
    def rindex(self, sub, start=None, end=None):
        pass
    
    def rjust(self, width, fillchar=None):
        pass
    
    def rpartition(self, sep):
        pass
    
    def rsplit(self, sep=None, maxsplit=None):
        pass
    
    def rstrip(self, chars=None):
        pass
    
    def split(self, sep=None, maxsplit=None):
        pass
    
    def splitlines(self, keepends=False):
        pass
    
    def startswith(self, prefix, start=None, end=None):
        pass
    
    def strip(self, chars=None):
        pass
    
    def swapcase(self):
        pass
    
    def title(self):
        pass
    
    def translate(self, table, deletechars=None):
        pass
    
    def upper(self):
        pass
    
    def zfill(self, width):
        pass

class unicode(str): # really inherits from basestring and duplicates str methods
    def isnumeric(self):
        pass
    
    def isdecimal(self):
        pass

class dict(object):
# was class UserDict:
    def __init__(self, dict=None, **kwargs):
    	pass
    def __repr__(self):
    	return repr(self.data)
    def __cmp__(self, dict):
    	return -1
    def __len__(self):
    	return 1
    def __getitem__(self, key):
        return self.data
    def __setitem__(self, key, item):
    	self.data = item
    	self.key = key
    def __delitem__(self, key):
    	pass
    def clear(self):
    	pass
    def copy(self):
        return dict()
    def keys(self):
    	return [self.key]
    def items(self):
    	return [(self.key, self.data)]
    def iteritems(self):
    	return self.items().__iter__()
    def iterkeys(self):
    	return self.keys().__iter__()
    def itervalues(self): 
    	return self.values().__iter__()
    def values(self):
    	return [self.data]
    def has_key(self, key):
    	return key == self.key
    def update(self, dict=None, **kwargs):
        #for k, v in dict.items():
        #    self[k] = v
    	pass
    def get(self, key, failobj=None):
        if key != self.key:
            return failobj
        return self.data
    def setdefault(self, key, failobj=None):
        if key != self.key:
            self.data = failobj
        return self.data
    def pop(self, key, *args):
        return self.data
    def popitem(self):
        return self.data
    def __contains__(self, key):
        return key == self.key

class list(object):
# was class UserList(collections.MutableSequence):
    def __init__(self, initlist=None):
    	pass
    
    def __repr__(self): return repr(self.data)
    def __lt__(self, other): return self.data <  self.__cast(other)
    def __le__(self, other): return self.data <= self.__cast(other)
    def __eq__(self, other): return self.data == self.__cast(other)
    def __ne__(self, other): return self.data != self.__cast(other)
    def __gt__(self, other): return self.data >  self.__cast(other)
    def __ge__(self, other): return self.data >= self.__cast(other)
    def __cast(self, other):
        if isinstance(other, list): return other.data
        else: return other
    def __cmp__(self, other):
        return cmp(self.data, self.__cast(other))
    def __contains__(self, item):
    	return self.data == item
    def __len__(self):
    	return 1
    def __getitem__(self, i):
    	return self.data
    def __setitem__(self, i, item):
    	self.data = item
    def __delitem__(self, i):
    	pass
    def __getslice__(self, i, j):
        return list()
    def __setslice__(self, i, j, other):
    	pass
    def __delslice__(self, i, j):
    	pass
    def __add__(self, other):
    	return list()
    def __radd__(self, other):
    	return list()
    def __iadd__(self, other):
        return self
    def __mul__(self, n):
        return list()
    def __rmul__(self, n):
    	return list()
    def __imul__(self, n):
        return self
    def append(self, item):
    	pass
    def insert(self, i, item):
    	pass
    def pop(self, i=-1):
    	return self.data
    def remove(self, item):
    	pass
    def count(self, item):
    	return 1
    def index(self, item, *args):
    	return 0
    def reverse(self):
    	pass
    def sort(self, *args, **kwds):
    	pass
    def extend(self, other):
    	pass
    
    # MutableSequence
    
    def __iter__(self):
        i = 0
        try:
            while True:
                v = self[i]
                yield v
                i += 1
        except IndexError:
            return
    
    def __reversed__(self):
        for i in reversed(range(len(self))):
            yield self[i]

# From pypy
class set(object):
#was class BaseSet(object):
    """Common base class for mutable and immutable sets."""
    
    def __init__(self, iterable=None):
        """Construct a set from an optional iterable."""
        self._data = {}
    
    # Standard protocols: __len__, __repr__, __str__, __iter__

    def __len__(self):
        """Return the number of elements of a set."""
        return len(self._data)
    
    def __repr__(self):
        """Return string representation of a set.

        This looks like 'Set([<list of elements>])'.
        """
        return '%s(%r)' % ("Set", self._data.keys())
    
    def __iter__(self):
        """Return an iterator over the elements or a set.

        This is the keys iterator for the underlying dict.
        """
        return self._data.iterkeys()
    
    def __cmp__(self, other):
        raise TypeError, "can't compare sets using cmp()"
    
    def __eq__(self, other):
        if isinstance(other, set):
            return self._data == other._data
        else:
            return False
    
    def __ne__(self, other):
        if isinstance(other, set):
            return self._data != other._data
        else:
            return True
    
    # Copying operations
    
    def copy(self):
        """Return a shallow copy of a set."""
        result = set()
        return result
    
    def __or__(self, other):
        """Return the union of two sets as a new set.

        (I.e. all elements that are in either set.)
        """
        return set()
    
    def union(self, other):
        """Return the union of two sets as a new set.

        (I.e. all elements that are in either set.)
        """
        return set()
    
    def __and__(self, other):
        """Return the intersection of two sets as a new set.

        (I.e. all elements that are in both sets.)
        """
        return set()
    
    def intersection(self, other):
        """Return the intersection of two sets as a new set.

        (I.e. all elements that are in both sets.)
        """
        return set()
    
    def __xor__(self, other):
        """Return the symmetric difference of two sets as a new set.

        (I.e. all elements that are in exactly one of the sets.)
        """
        return set()
    
    def symmetric_difference(self, other):
        """Return the symmetric difference of two sets as a new set.

        (I.e. all elements that are in exactly one of the sets.)
        """
        return set()
    
    def  __sub__(self, other):
        """Return the difference of two sets as a new Set.

        (I.e. all elements that are in this set and not in the other.)
        """
        return set()
    
    def difference(self, other):
        """Return the difference of two sets as a new Set.

        (I.e. all elements that are in this set and not in the other.)
        """
        return set()
    
    # Membership test
    
    def __contains__(self, element):
        """Report whether an element is a member of a set.

        (Called in response to the expression `element in self'.)
        """
        return element in self._data
    
    # Subset and superset test
    
    def issubset(self, other):
        """Report whether another set contains this set."""
        return element in self._data
    
    def issuperset(self, other):
        """Report whether this set contains another set."""
        return element not in self._data
    
    # Inequality comparisons using the is-subset relation.
    __le__ = issubset
    __ge__ = issuperset
    
    def __lt__(self, other):
        return len(self) < len(other) and self.issubset(other)
    
    def __gt__(self, other):
        return len(self) > len(other) and self.issuperset(other)
    
    # In-place union, intersection, differences.
    # Subtle:  The xyz_update() functions deliberately return None,
    # as do all mutating operations on built-in container types.
    # The __xyz__ spellings have to return self, though.
    
    def __ior__(self, other):
        """Update a set with the union of itself and another."""
        return self
    
    def union_update(self, other):
        """Update a set with the union of itself and another."""
        pass
    
    def __iand__(self, other):
        """Update a set with the intersection of itself and another."""
        return self
    
    def intersection_update(self, other):
        """Update a set with the intersection of itself and another."""
        pass
    
    def __ixor__(self, other):
        """Update a set with the symmetric difference of itself and another."""
        return self
    
    def symmetric_difference_update(self, other):
        """Update a set with the symmetric difference of itself and another."""
        pass
    
    def __isub__(self, other):
        """Remove all elements of another set from this set."""
        return self
    
    def difference_update(self, other):
        """Remove all elements of another set from this set."""
        pass
    
    # Python dict-like mass mutations: update, clear
    
    def update(self, iterable):
        """Add all values from an iterable (such as a list or file)."""
        pass
    
    def clear(self):
        """Remove all elements from this set."""
        pass
    
    # Single-element mutations: add, remove, discard
    
    def add(self, element):
        """Add an element to a set.

        This has no effect if the element is already present.
        """
        self._data[element] = True
    
    def remove(self, element):
        """Remove an element from a set; it must be a member.

        If the element is not a member, raise a KeyError.
        """
        del self._data[element]
    
    def discard(self, element):
        """Remove an element from a set if it is a member.

        If the element is not a member, do nothing.
        """
        self.remove(element)
    
    def pop(self):
        """Remove and return an arbitrary set element."""
        return self._data[0]

# We use this class at a dummy type for None because it is really something
# define in the interpreter alone.  The NoneType in 'types' is defined
# as 'NoneType = type(None)' which is pretty circular reasoning
class __BuiltinNoneType__:
	pass

None = __BuiltinNoneType__()