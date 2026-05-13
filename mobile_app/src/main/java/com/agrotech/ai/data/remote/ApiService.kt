package com.agrotech.ai.data.remote

import com.agrotech.ai.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("iot/connect")
    suspend fun connectDevice(@Body request: Map<String, String>): Response<Map<String, Any>>

    @GET("weather/current")
    suspend fun getCurrentWeather(@Query("lat") lat: Double, @Query("lon") lon: Double): Response<WeatherData>

    @POST("recommend/crop")
    suspend fun getCropRecommendation(@Body data: SoilData): Response<RecommendationResponse>

    @POST("recommend/fertilizer")
    suspend fun getFertilizerRecommendation(@Body data: FertilizerRequest): Response<RecommendationResponse>

    @POST("detect/stress")
    suspend fun detectStress(@Body request: Map<String, String>): Response<StressDetectionResponse>

    @POST("ai/ask")
    suspend fun queryChatbot(@Body request: Map<String, String>): Response<Map<String, String>>

    @GET("iot/latest")
    suspend fun getLatestIot(): Response<IotResponse>

    @GET("iot")
    suspend fun simulateIot(@Query("soil") soil: Double, @Query("temp") temp: Double): Response<Map<String, Any>>

    @POST("analyze-crop")
    suspend fun analyzeCrop(@Body request: CropAnalysisRequest): Response<CropAnalysisResponse>

    @GET("recommend/future")
    suspend fun getFutureRecommendation(
        @Query("lat") lat: Double, 
        @Query("lon") lon: Double, 
        @Query("days") days: Int,
        @Query("lang") lang: String,
        @Query("n") n: Float,
        @Query("p") p: Float,
        @Query("k") k: Float,
        @Query("ph") ph: Float
    ): Response<RecommendationResponse>
}
