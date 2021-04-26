package com.shop.springboot.service;

import com.shop.springboot.dto.ProductOrderDto.ProductOrderResponseDto;
import com.shop.springboot.dto.pagingDto.PagingDto;
import com.shop.springboot.entity.Cart;
import com.shop.springboot.entity.ProductOrder;
import com.shop.springboot.entity.User;
import com.shop.springboot.exception.NotExistOrderException;
import com.shop.springboot.exception.NotExistProductException;
import com.shop.springboot.exception.NotExistUserException;
import com.shop.springboot.repository.CartRepository;
import com.shop.springboot.repository.ProductOrderRepository;
import com.shop.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class ProductOrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductOrderRepository productOrderRepository;

    // 주문 조회
    public ProductOrderResponseDto findOne(Long orderId) {

        Optional<ProductOrder> orderOpt = productOrderRepository.findById(orderId);

        if (!orderOpt.isPresent())
            throw new NotExistOrderException("존재하지 않는 주문입니다.");

        return orderOpt.get().toResponseDto();
    }

    public Long save(ProductOrder productOrder) {
        User user = userRepository.findById(productOrder.getUser().getId()).orElseThrow(()
                -> new NotExistUserException("존재하지 않는 유저입니다."));

        List<Cart> carts = cartRepository.findAllByUserIdOrderByCreatedTimeDesc(user.getId());

        if(carts.isEmpty())
            throw new NotExistProductException("장바구니에 상품이 없습니다.");

        productOrder.setCarts(carts);

        return productOrderRepository.save(productOrder).getId();

    }

    // 주문 리스트 조회
    public HashMap<String, Object> findProductOrders(Long userId, int page) {
        int realPage = (page == 0) ? 0 : (page - 1);
        PageRequest pageable = PageRequest.of(realPage, 5);

        Page<ProductOrder> productOrderPage = productOrderRepository.findAllByUserIdOrderByCreatedTimeDesc(userId, pageable);

        if (productOrderPage.getTotalElements() > 0) {
            List<ProductOrderResponseDto> productOrderResponseDtoList = new ArrayList<>();

            for (ProductOrder productOrder : productOrderPage) {
                productOrderResponseDtoList.add(productOrder.toResponseDto());
            }

            PageImpl<ProductOrderResponseDto> productOrderResponseDtos
                    = new PageImpl<>(productOrderResponseDtoList, pageable, productOrderPage.getTotalElements());

            PagingDto productOrderPagingDto = new PagingDto();
            productOrderPagingDto.setPagingInfo(productOrderResponseDtos);

            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("productOrderList", productOrderResponseDtos);
            resultMap.put("productOrderPagingDto", productOrderPagingDto);

            return resultMap;
        }

        return null;
    }

    // 주문 삭제
    public void delete(Long productOrderId) {
        productOrderRepository.deleteById(productOrderId);
    }

}