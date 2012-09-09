#!/usr/bin/perl -w

# A terrible little perl script to generate xliff:doc <g> placeholders based
# on an HTML tag.
#
# Usage: ./convert.pl '<a href="...">blah</a>'

my $raw = join " ", @ARGV;

my $gid = 1;

my $result = "";

while ($raw ne "") {
    if ($raw =~ /^(<.*?>)/) {
        my $tag = $1;
        $raw =~ s/^<.*?>//;
        
        my $isOpen = 1;
        if ($tag =~ /^<\/(\w+)>/) {
            $isOpen = 0;
        }
        if ($isOpen) {
            my $element = undef;
            if ($tag =~ /^<(\w+)/) {
                $element = $1;
            }
            $tag =~ s/</&lt;/g;
            $tag =~ s/>/&gt;/g;
            $tag =~ s/"/&quot;/g;

            $result .= "<g id=\"$gid\" ";
            $result .= "ctype=\"x-other\" dx:orig-markup-open=\"$tag\"";
            if (defined $element) {
                $result .= " dx:orig-markup-close=\"&lt;/$element&gt;\"";
            }
            $result .= ">";
        }
        else {
            $result .= "</g>";
        }
    }
    else {
        $raw =~ /^([^<]+)/;
        $result .= $1;
        $raw =~ s/^[^<]+//;
    }
}
print "$result\n";
