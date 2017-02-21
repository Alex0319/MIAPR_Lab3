package by.miapr.algorithm;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * Created by user1 on 15.02.2017.
 */
public class ClassificationDeterminant {
    private final int scaleMode=200000;
    private int experimentsCount;
    private static ClassificationDeterminant instance;
    private static Random rand=new Random(new Date().getTime());
    private int[] firstExperimentArray;
    private int[] secondExperimentArray;

    private float firstProbability,secondProbability;
    private double divideLine;
    private double firstExpectedValue,secondExpectedValue;
    private double firstStandartDeviation,secondStandartDeviation;
    private double errorFalseAlarm,errorMissingDetecting;

    private ClassificationDeterminant(){};

    public static ClassificationDeterminant getInstance(){
        if(instance==null){
            instance=new ClassificationDeterminant();
        }
        return instance;
    }

    private void initValues(int initValue){
        experimentsCount=initValue;
        errorFalseAlarm=0;
        errorMissingDetecting=0;
        firstExperimentArray=new int[initValue];
        secondExperimentArray=new int[initValue];
    }

    private void setExperimentsValues(int maxValue,int offset){
        for(int i=0;i<experimentsCount;i++){
            firstExperimentArray[i]=rand.nextInt(maxValue)-offset;
            secondExperimentArray[i]=rand.nextInt(maxValue)+offset;
        }
    }

    private double getExpectedValue(int[] experimentsArray,int experimentsCount){
        return Arrays.stream(experimentsArray).sum()/experimentsCount;
    }

    private double getStandartDeviation(int[] experimentsArray,int experimentsCount,double expectedValue){
        double result=0;
        for(int i=0;i<experimentsCount;i++){
            result+=Math.pow(experimentsArray[i]-expectedValue,2);
        }
        return Math.sqrt(result/experimentsCount);
    }

    private double getGaussianDistribution(double expectedValue, double standartDeviation,double x){
        return Math.exp(-0.5 * Math.pow((x - expectedValue) / standartDeviation,2))/(standartDeviation * Math.sqrt(2 * Math.PI));
    }

    private double getFalseAlarmError(double x){
        double firstGaussianDistribution,secondGaussianDistribution;
        do{
           firstGaussianDistribution=getGaussianDistribution(firstExpectedValue,firstStandartDeviation,x);
           secondGaussianDistribution=getGaussianDistribution(secondExpectedValue,secondStandartDeviation,x);
           errorFalseAlarm+=secondGaussianDistribution*0.001;
           x += 0.001;
        }while(secondGaussianDistribution*secondProbability<firstGaussianDistribution*firstProbability);
        return x;
    }

    private void searchMissingDetectingError(int width) {
        double gaussianDistribution;
        double x=divideLine;
        while (x<width){
            gaussianDistribution=getGaussianDistribution(firstExpectedValue,firstStandartDeviation,x);
            errorMissingDetecting+=gaussianDistribution*firstProbability*0.001;
            x += 0.001;
        }
    }

    private void drawGraph(GraphicsContext g,Color color,int width,int height,double expectedValue,double standartDeviation,float probability){
        for(int i=0;i<experimentsCount;i++){
            Color oldStrokeColor=(Color)g.getStroke();
            double currentGaussianDistribution=getGaussianDistribution(expectedValue,standartDeviation,i);
            int currentY=height-(int)(currentGaussianDistribution*probability*scaleMode);
            g.setStroke(color);
            g.strokeRect(i,currentY,width,height+1000);
            g.setStroke(oldStrokeColor);
        }
    }

    private void drawDivideLine(GraphicsContext g, Color color,int height) {
        Color oldStrokeColor=(Color)g.getStroke();
        g.setStroke(color);
        g.strokeLine(divideLine,0,divideLine,height);
        g.setStroke(oldStrokeColor);
    }

    private void drawGraphics(GraphicsContext g,int width,int height){
        g.clearRect(0,0,width,height);
        drawGraph(g,Color.BLACK,10,height,firstExpectedValue,firstStandartDeviation,firstProbability);
        drawGraph(g,Color.BROWN,3,height,secondExpectedValue,secondStandartDeviation,secondProbability);
        drawDivideLine(g,Color.BLUE,height);
    }

    public double getErrorFalseAlarm() {
        return errorFalseAlarm;
    }

    public double getErrorMissingDetecting() {
        return errorMissingDetecting;
    }

    public double getSumError(){
        return errorFalseAlarm+errorMissingDetecting;
    }

    public void setClassification(int width,int height, GraphicsContext g,float firstProbability,float secondProbability){
        final int offset=150;
        initValues(width);
        setExperimentsValues(width,offset);
        this.firstProbability=firstProbability;
        this.secondProbability=secondProbability;
        firstExpectedValue=getExpectedValue(firstExperimentArray,experimentsCount);
        secondExpectedValue=getExpectedValue(secondExperimentArray,experimentsCount);
        firstStandartDeviation=getStandartDeviation(firstExperimentArray,experimentsCount,firstExpectedValue);
        secondStandartDeviation=getStandartDeviation(secondExperimentArray,experimentsCount,secondExpectedValue);
        divideLine=getFalseAlarmError(Math.negateExact(offset));
        searchMissingDetectingError(width);
        drawGraphics(g,width,height);
    }
}