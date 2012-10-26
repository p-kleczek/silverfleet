package sfmainframe;

public class Range {

    private int lowerBound;
    private int upperBound;


    public Range(int low, int high) {
        this.lowerBound = low;
        this.upperBound = high;
    }


    public int getLowerBound() {
        return lowerBound;
    }


    public int getUpperBound() {
        return upperBound;
    }


    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }


    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

}